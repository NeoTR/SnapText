const express = require("express");
const bodyParser = require("body-parser");
const mongoose = require("mongoose");
const boxSchema = require("./models/box.js");
const cors = require("cors");

require("dotenv").config();

const uri = process.env.uri;

async function connect() {
  try {
    await mongoose.connect(uri);
    console.log("Connected to the database!");
  } catch (err) {
    console.log(err);
  }
}

connect();

const app = express();

app.use(bodyParser.json());
app.use(cors());

app.post("/api", (req, res) => {
  const box = new boxSchema({
    text: req.body.text,
    date: req.body.date,
  });
  box.save();

  res.json({ message: "Box added successfully!" });
});

app.get("/api/getBoxes", async (req, res) => {
  const boxes = await boxSchema.find();
  res.json(boxes);
});

app.listen(3000, () => {
  console.log("Server is listening on port 3000. Ready to accept requests!");
});
