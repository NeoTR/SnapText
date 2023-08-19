const weekColumn = document.querySelector(".week");

console.log("JS Started");

const columnCounts = {
  week: 0,
  month: 0,
  other: 0,
};

fetch("http://localhost:3000/api/getBoxes")
  .then((response) => response.json())
  .then((data) => {
    data.forEach((box) => {
      addBoxToColumn(box.text, box.date);
    });
  });

function addBoxToColumn(text, date) {
  console.log(date);
  let editedText;
  if (text.length > 75) {
    editedText = text.slice(0, 60) + "...";
  } else {
    editedText = text;
  }

  const boxDate = new Date(date);
  const currentDate = new Date();

  let columnClass;

  if (boxDate >= currentDate - 7 * 24 * 60 * 60 * 1000) {
    columnClass = "week";
  } else if (boxDate >= currentDate - 30 * 24 * 60 * 60 * 1000) {
    columnClass = "month";
  } else {
    columnClass = "other";
  }

  const column = document.querySelector(`.${columnClass}`);

  const newBox = document.createElement("div");
  newBox.classList.add("box", `${columnClass}-${++columnCounts[columnClass]}`);
  newBox.setAttribute("data-copies", "0");

  const tag = document.createElement("span");
  tag.classList.add("tag");
  tag.id = "blue";
  tag.textContent = "SnapText";
  newBox.appendChild(tag);

  const boxText = document.createElement("p");
  boxText.textContent = editedText;
  newBox.setAttribute("data-text", text);
  newBox.appendChild(boxText);

  const footer = document.createElement("div");
  footer.classList.add("box-footer");
  newBox.appendChild(footer);

  const dateDiv = document.createElement("div");
  dateDiv.classList.add("date");
  footer.appendChild(dateDiv);
  const dateIcon = document.createElement("li");
  const dateIconI = document.createElement("i");
  dateIconI.classList.add("fa-solid", "fa-calendar-days");
  dateIcon.appendChild(dateIconI);
  dateDiv.appendChild(dateIcon);

  const dateText = document.createElement("span");
  dateText.textContent = date;
  dateDiv.appendChild(dateText);

  const views = document.createElement("li");
  views.classList.add("views");
  footer.appendChild(views);

  const viewsIcon = document.createElement("i");
  viewsIcon.classList.add("fa-sharp", "fa-solid", "fa-eye");
  views.appendChild(viewsIcon);

  // use span for viewsText
  const viewsText = document.createElement("span");
  viewsText.textContent = "0";
  views.appendChild(viewsText);

  column.appendChild(newBox);

  document.querySelector(".week .num").textContent = columnCounts.week;
  document.querySelector(".month .num").textContent = columnCounts.month;
  document.querySelector(".other .num").textContent = columnCounts.other;
}
document.addEventListener("click", (event) => {
  const box = event.target.closest(".box");
  if (box && event.button === 0) {
    const text = box.getAttribute("data-text");
    const copies = parseInt(box.getAttribute("data-copies")) + 1;
    box.setAttribute("data-copies", copies.toString());
    const viewsText = box.querySelector(".views span");
    if (viewsText) {
      viewsText.textContent = copies.toString();
      navigator.clipboard
        .writeText(text)
        .then(() => alert("Copied text to clipboard!"))
        .catch((error) => alert("Failed to copy text to clipboard: " + error));
    }
  }
});

addBoxToColumn("This is a test box!", "8/10/2023");
addBoxToColumn("This is a test box!", "7/10/2023");
addBoxToColumn("This is a test box!", "7/2/2023");
