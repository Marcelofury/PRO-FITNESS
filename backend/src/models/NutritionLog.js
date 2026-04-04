const mongoose = require('mongoose');

const nutritionLogSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    mealName: {
      type: String,
      required: true,
      trim: true,
    },
    calories: {
      type: Number,
      required: true,
      min: 0,
    },
    proteinGrams: {
      type: Number,
      default: 0,
      min: 0,
    },
    carbsGrams: {
      type: Number,
      default: 0,
      min: 0,
    },
    fatGrams: {
      type: Number,
      default: 0,
      min: 0,
    },
    loggedAt: {
      type: Date,
      default: Date.now,
    },
  },
  { timestamps: true }
);

module.exports = mongoose.model('NutritionLog', nutritionLogSchema);
