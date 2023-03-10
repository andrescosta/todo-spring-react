import React from "react";

export default function Modal({ children, shown, close }) {
    return shown ? (
        <div id="modalBook" className="w3-modal" style={{display:'block'}}
            onClick={() => {
                close();
            }}>
            <div className="w3-modal-content" 
                onClick={e => {
                    e.stopPropagation();
                }}>
                <span onClick={close} className="w3-button w3-display-topright w3-light-grey">&times;</span>
                {children}
            </div>
        </div>
    ) : null;
}