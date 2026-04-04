const express = require('express');
const auth = require('../middleware/auth');
const User = require('../models/User');

const router = express.Router();

router.put('/me', auth, async (req, res) => {
  try {
    const allowedFields = ['name', 'age', 'heightCm', 'weightKg', 'goal'];
    const updates = {};

    for (const key of allowedFields) {
      if (Object.prototype.hasOwnProperty.call(req.body, key)) {
        updates[key] = req.body[key];
      }
    }

    const user = await User.findByIdAndUpdate(req.user._id, updates, {
      new: true,
      runValidators: true,
    });

    const userObject = user.toObject();
    delete userObject.passwordHash;

    return res.json({ success: true, data: userObject });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to update user profile' });
  }
});

module.exports = router;
