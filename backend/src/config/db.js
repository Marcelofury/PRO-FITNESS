const mongoose = require('mongoose');

function isPlaceholderMongoUri(uri) {
  return uri.includes('<username>') || uri.includes('<password>') || uri.includes('<cluster-name>');
}

async function connectDB() {
  const uri = process.env.MONGODB_URI;

  if (!uri) {
    throw new Error('MONGODB_URI is not set. Create backend/.env from backend/.env.example and set a real MongoDB URI.');
  }

  if (isPlaceholderMongoUri(uri)) {
    throw new Error('MONGODB_URI still contains placeholders. Replace <username>, <password>, and <cluster-name> in backend/.env.');
  }

  await mongoose.connect(uri);
  console.log('MongoDB connected');
}

module.exports = connectDB;
