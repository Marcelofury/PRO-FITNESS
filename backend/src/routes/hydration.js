const express = require('express');
const auth = require('../middleware/auth');
const HydrationLog = require('../models/HydrationLog');

const router = express.Router();

router.use(auth);

router.get('/', async (req, res) => {
  try {
    const logs = await HydrationLog.find({ userId: req.user._id }).sort({ loggedAt: -1 }).limit(100);
    return res.json({ success: true, data: logs });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to fetch hydration logs' });
  }
});

router.get('/today-total', async (req, res) => {
  try {
    const start = new Date();
    start.setHours(0, 0, 0, 0);

    const aggregate = await HydrationLog.aggregate([
      { $match: { userId: req.user._id, loggedAt: { $gte: start } } },
      { $group: { _id: null, totalMl: { $sum: '$amountMl' } } },
    ]);

    const totalMl = aggregate.length ? aggregate[0].totalMl : 0;
    return res.json({ success: true, data: { totalMl } });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to calculate hydration total' });
  }
});

router.post('/', async (req, res) => {
  try {
    const { amountMl, loggedAt } = req.body;

    if (!Number.isFinite(amountMl) || amountMl <= 0) {
      return res.status(400).json({ success: false, message: 'amountMl must be a positive number' });
    }

    const log = await HydrationLog.create({
      userId: req.user._id,
      amountMl,
      loggedAt: loggedAt || new Date(),
    });

    return res.status(201).json({ success: true, data: log });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to create hydration log' });
  }
});

module.exports = router;
