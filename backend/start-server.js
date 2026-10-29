const { MongoMemoryServer } = require('mongodb-memory-server');

async function start() {
  try {
    console.log('🔄 Starting embedded MongoDB...');
    const mongod = await MongoMemoryServer.create({
      instance: {
        port: 27017,
        dbName: 'bloodbank'
      }
    });
    const uri = mongod.getUri();
    console.log('✅ Embedded MongoDB started at:', uri);
    process.env.MONGODB_URI = uri;
  } catch (err) {
    console.log('⚠️ Could not start embedded MongoDB, falling back to default URI:', err.message);
  }

  // Start the main application
  require('./src/index.js');
}

start();

