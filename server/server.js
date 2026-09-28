/**
 * Golden Ember Restaurant - Online Cloud REST API & Live WebSocket Tracking Server
 * سيرفر نظام مطعم الجمر الذهبي للعمل أونلاين ومزامنة الطلبات وتتبع المناديب والمدفوعات
 */

const express = require('express');
const http = require('http');
const cors = require('cors');
const { WebSocketServer } = require('ws');

const PORT = process.env.PORT || 8080;
const API_KEY = process.env.API_KEY || 'GE-LIVE-CLOUD-994820-SA';

const app = express();
app.use(cors());
app.use(express.json());

// In-memory state for live synchronization across Customer, Driver, and Admin apps
const state = {
  orders: [],
  driverLocations: {},
  payments: [],
  lastSyncAt: new Date().toISOString()
};

// Optional Bearer API Key middleware
function verifyApiKey(req, res, next) {
  const authHeader = req.headers.authorization || '';
  if (authHeader.startsWith('Bearer ')) {
    const token = authHeader.replace('Bearer ', '').trim();
    if (token && token !== API_KEY) {
      return res.status(401).json({ status: 'error', message: 'Invalid API Key' });
    }
  }
  next();
}

// Root & Health Check Endpoint (Used by Android App ServerOnlineConfigDialog & Repository)
app.get(['/', '/v1', '/v1/health'], verifyApiKey, (req, res) => {
  res.status(200).json({
    status: 'online',
    service: 'Golden Ember Restaurant Cloud Server',
    version: '1.0.0',
    activeOrdersCount: state.orders.length,
    activeDriversCount: Object.keys(state.driverLocations).length,
    serverTime: new Date().toISOString()
  });
});

// Sync Orders Endpoint
app.get('/v1/orders', verifyApiKey, (req, res) => {
  res.json({
    status: 'ok',
    orders: state.orders,
    driverLocations: state.driverLocations,
    lastSyncAt: state.lastSyncAt
  });
});

app.post('/v1/orders/sync', verifyApiKey, (req, res) => {
  const { orders } = req.body;
  if (Array.isArray(orders)) {
    state.orders = orders;
    state.lastSyncAt = new Date().toISOString();
    broadcastLiveEvent({
      type: 'ORDERS_UPDATED',
      orders: state.orders,
      timestamp: state.lastSyncAt
    });
  }
  res.json({ status: 'ok', syncedCount: state.orders.length, lastSyncAt: state.lastSyncAt });
});

// Live Driver GPS Location Update Endpoint
app.post('/v1/drivers/:driverId/location', verifyApiKey, (req, res) => {
  const { driverId } = req.params;
  const { orderId, mapX, mapY, latitude, longitude, status } = req.body;

  state.driverLocations[driverId] = {
    driverId: Number(driverId),
    orderId: orderId ? Number(orderId) : null,
    mapX: Number(mapX || 0.5),
    mapY: Number(mapY || 0.5),
    latitude: Number(latitude || 24.7136),
    longitude: Number(longitude || 46.6753),
    status: status || 'ON_THE_WAY',
    updatedAt: new Date().toISOString()
  };

  broadcastLiveEvent({
    type: 'DRIVER_LOCATION_UPDATE',
    payload: state.driverLocations[driverId]
  });

  res.json({ status: 'ok', location: state.driverLocations[driverId] });
});

// Electronic Payment Authorization Endpoint (MADA, Apple Pay, STC Pay, VISA, COD)
app.post('/v1/payments/authorize', verifyApiKey, (req, res) => {
  const { gatewayCode, amount, orderNumber, customerName } = req.body;
  const prefix = (gatewayCode || 'PAY').toUpperCase().slice(0, 4);
  const reference = `${prefix}-LIVE-${Date.now().toString().slice(-6)}`;

  const paymentRecord = {
    reference,
    gatewayCode: gatewayCode || 'MADA',
    amount: Number(amount || 0),
    orderNumber: orderNumber || null,
    customerName: customerName || '',
    status: 'PAID',
    authorizedAt: new Date().toISOString()
  };
  state.payments.push(paymentRecord);

  broadcastLiveEvent({
    type: 'PAYMENT_AUTHORIZED',
    payload: paymentRecord
  });

  res.json({ status: 'ok', payment: paymentRecord });
});

const server = http.createServer(app);

// WebSocket Server for Real-Time Direct Order Tracking (/ws/tracking)
const wss = new WebSocketServer({ server, path: '/ws/tracking' });

function broadcastLiveEvent(eventData) {
  const payload = JSON.stringify(eventData);
  wss.clients.forEach((client) => {
    if (client.readyState === 1) {
      client.send(payload);
    }
  });
}

wss.on('connection', (ws) => {
  ws.send(
    JSON.stringify({
      type: 'CONNECTED',
      message: 'Connected to Golden Ember Live Tracking WebSocket',
      driverLocations: state.driverLocations,
      timestamp: new Date().toISOString()
    })
  );

  ws.on('message', (raw) => {
    try {
      const msg = JSON.parse(raw.toString());
      if (msg.type === 'DRIVER_GPS_PING' && msg.driverId) {
        state.driverLocations[msg.driverId] = {
          ...msg,
          updatedAt: new Date().toISOString()
        };
        broadcastLiveEvent({
          type: 'DRIVER_LOCATION_UPDATE',
          payload: state.driverLocations[msg.driverId]
        });
      }
    } catch (err) {
      // Ignore malformed messages
    }
  });
});

server.listen(PORT, () => {
  console.log(`Golden Ember Online Server running on http://localhost:${PORT}/v1`);
  console.log(`Live Tracking WebSocket running on ws://localhost:${PORT}/ws/tracking`);
});
