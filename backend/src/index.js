require('dotenv').config();
const express = require('express');
const cors = require('cors');
const connectDB = require('./config/database');

// Import routes
const authRoutes = require('./routes/auth');
const donorRoutes = require('./routes/donors');
const requestRoutes = require('./routes/requests');
const donationRoutes = require('./routes/donations');
const chatRoutes = require('./routes/chat');
const contactRequestRoutes = require('./routes/contactRequests');
const adminRoutes = require('./routes/admin');

const app = express();
const PORT = process.env.PORT || 3000;

// Connect to MongoDB
connectDB();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
  console.log(`\n📞 ${req.method} ${req.url}`);
  console.log('⏰ Time:', new Date().toLocaleString());
  if (Object.keys(req.body).length > 0) {
    console.log('📦 Body:', req.body);
  }
  next();
});

// Static folder for uploads
app.use('/uploads', express.static('uploads'));

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/donors', donorRoutes);
app.use('/api/requests', requestRoutes);
app.use('/api/donations', donationRoutes);
app.use('/api/chat', chatRoutes);
app.use('/api/contact-requests', contactRequestRoutes);
app.use('/api/notifications', require('./routes/notifications'));
app.use('/api/admin', adminRoutes);

// Health check
app.get('/', (req, res) => {
  res.json({
    message: 'Blood Bank API is running!',
    version: '1.0.0',
    status: 'healthy'
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(err.status || 500).json({
    success: false,
    message: err.message || 'Internal Server Error',
    error: process.env.NODE_ENV === 'development' ? err : {}
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({                                                                                                                                                                 
    success: false,
    message: 'Route not found'
  });
});

const os = require('os');

function getLocalIp() {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  return 'localhost';
}

app.listen(PORT, () => {
  const localIp = getLocalIp();
  console.log('\n🎉 ========================================');
  console.log('🩸 Blood Bank API Server Started!');
  console.log('========================================');
  console.log(`📍 Local: http://localhost:${PORT}`);
  console.log(`🌐 Wi-Fi Network: http://${localIp}:${PORT}`);
  console.log('📝 Endpoints:');
  console.log('   - POST /api/auth/register');
  console.log('   - POST /api/auth/login');
  console.log('   - GET  /api/donors');
  console.log('   - GET  /api/requests');
  console.log('   - POST /api/requests (with auth)');
  console.log('   - GET  /api/donations');
  console.log('   - POST /api/donations (with auth)');
  console.log('========================================\n');
  console.log('✅ Waiting for requests...\n');
});

// Force restart to pick up changes - 2
