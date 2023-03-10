import React from "react"

export function Header() {
    return (
        <>
            <header className="w3-container" style={{ paddingTop: 22 + 'px' }}>
                <h5><b><i className="fa fa-address-book-o"></i>MyTODOs</b></h5>
            </header>

        </>)
}

export function Footer() {
    return (<>
        <footer className="w3-container w3-padding-16 w3-light-grey">
            <p>Powered by <a href="https://www.w3schools.com/w3css/default.asp" target="_blank">w3.css</a></p>
        </footer>
    </>)
}
