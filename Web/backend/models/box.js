const mongoose = require("mongoose");

const boxSchema = new mongoose.Schema(
  {
    text: {
      type: String,
      required: true,
    },
    date: {
      type: String,
      required: true,
    },
  },
  { timestamps: true }
);

const Box = mongoose.model("boxes", boxSchema);

module.exports = Box;
