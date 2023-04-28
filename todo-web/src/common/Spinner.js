import styles from "../App.css";
import React from "react";
export default function LoadingSpinner() {

    return (
        <div className="lds">
            <div></div>
            <div></div>
            <div></div>
            <div></div>
        </div>
    );
};