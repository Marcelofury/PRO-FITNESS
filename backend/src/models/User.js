const mongoose = require('mongoose');

const userSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: true,
      trim: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    passwordHash: {
      type: String,
      required: true,
    },
    age: {
      type: Number,
      min: 1,
      max: 120,
      default: null,
    },
    heightCm: {
      type: Number,
      min: 50,
      max: 300,
      default: null,
    },
    weightKg: {
      type: Number,
      min: 10,
      max: 500,
      default: null,
    },
    goal: {
      type: String,
      default: '',
      trim: true,
    },
  },
  { timestamps: true }
);

module.exports = mongoose.model('User', userSchema);
