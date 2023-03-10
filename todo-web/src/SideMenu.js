import React from "react"
import { useAuthContext } from "./AuthContextProvider"
export default function SideMenu({menuShown}) {
    const authContext = useAuthContext();
    return menuShown ? (
        <>
            <div className="w3-sidebar w3-bar-block w3-dark-grey w3-animate-left" id="mySidebar"><br></br>
                <div className="w3-container">
                    <h5>Menu</h5>
                </div>
                <div className="w3-bar-block">
                    <a href="#" className="w3-bar-item w3-button w3-padding w3-blue"><i className="fa fa-users fa-fw"></i>
                        All TODOs</a>
                    <a href="#" className="w3-bar-item w3-button w3-padding"><i className="fa fa-hashtag fa-fw"></i>  Label 1</a>
                    <a href="#" className="w3-bar-item w3-button w3-padding"><i className="fa fa-hashtag fa-fw"></i>  Label 2</a>
                    <a href="#" className="w3-bar-item w3-button w3-padding"><i className="fa fa-hashtag fa-fw"></i>  Label 3</a>
                    <a href="#" className="w3-bar-item w3-button w3-padding"><i className="fa fa-trash fa-fw"></i>  Deleted</a>
                    <a href="#" className="w3-bar-item w3-button w3-padding"><i className="fa fa-history fa-fw"></i>  Archived</a>
                    <a href="#" className="w3-bar-item w3-button w3-padding"><i className="fa fa-cog fa-fw"></i>
                        Settings</a>
                    <a href="#" className="w3-bar-item w3-button w3-padding" onClick={()=>authContext.logout()}><i className="fa fa-sign-out fa-fw"></i>
                        Logout</a><br></br><br></br>
                </div>
            </div>

        </>) : null;
}
