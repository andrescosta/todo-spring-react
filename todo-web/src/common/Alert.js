import React from "react";

export default function Alert({ type, title, message }) {
    function closeAlert() {
      this.parentElement.style.display = 'none'
    }
  
    let classes = "w3-panel w3-display-container"
    switch (type) {
      case 0:
      default:
        classes += " w3-red";
        break;

    }


    return <>
      <div className={classes}>
        <span onClick={() => closeAlert()}
          className="w3-button w3-large w3-display-topright">&times;</span>
        <h3>{title}</h3>
        <p>{message}</p>
      </div>
    </>
  }
