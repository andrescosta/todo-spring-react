import React, { useState } from "react";
import { useActivitiesDispatch, Events } from "../activity/ActivitiesContext"
import ActivityForm from "../activity/ActivityForm";
import Modal from "../common/Modal"


export default function ToolBar() {

    let dispatch = useActivitiesDispatch();

    const [modalShown, toggleModal] = useState(false);
    const [selectedOption, toggleSelectedOption] = useState("HOME");

    async function onClickFilterByType(type) {
        dispatch({ type: Events.FILTEREDBYTYPE, typeActivity: type });
        toggleSelectedOption(type)
    }
    async function onClickHome() {
        dispatch({ type: Events.FILTERRESETED });
        toggleSelectedOption("HOME")
    }

    function selectedClass(type) {
        if (selectedOption === type) {
            return "w3-dark-grey";
        } else {
            return "";
        }
    }



    return (
        <>
            <Modal
                shown={modalShown}
                close={() => {
                    toggleModal(false);
                }}>
                <ActivityForm toggleModal={toggleModal}></ActivityForm>
            </Modal>
            <div className="w3-bar w3-light-grey w3-border">
                <a href="#" onClick={() => onClickHome()} className={"w3-bar-item w3-button " + selectedClass("HOME")} ><i className="fa fa-home"></i></a>
                <a href="#" onClick={() => onClickFilterByType("LINK")} className={"w3-bar-item w3-button " + selectedClass("LINK")}><i className="fa fa-link"></i></a>
                <a href="#" onClick={() => onClickFilterByType("BOOK")} className={"w3-bar-item w3-button " + selectedClass("BOOK")}><i className="fa fa-book"></i></a>
                <a href="#" onClick={() => onClickFilterByType("MOVIE")} className={"w3-bar-item w3-button " + selectedClass("MOVIE")}><i className="fa fa-file-movie-o"></i></a>
                <a href="#" onClick={() => Main()} className="w3-bar-item w3-button w3-black" style={{ marginLeft: 5 + 'px' }}><i
                    className="fa fa-refresh"></i></a>
                <a href="#" className="w3-bar-item w3-button w3-black" style={{ marginLeft: 5 + 'px' }}><i
                    className="fa fa-search"></i></a>
                <a href="#" onClick={() => {
                    toggleModal(!modalShown);
                }} className="w3-bar-item w3-button w3-black" style={{ marginLeft: 5 + 'px' }}><i className="fa fa-plus"></i></a>
            </div>

        </>)
}
