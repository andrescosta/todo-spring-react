import { deleteActivity } from "./ActivitiesService"
import { useActivitiesDispatch } from "./ActivitiesContext";
import React from "react";
import { Events } from "./ActivitiesContext";


export default function ActivityCard({ type, name, description, id }) {
    let icon = "fa";
    let dispatch = useActivitiesDispatch();

    switch (type) {
      case ("LINK"):
        icon += " fa-link";
        break;
      case ("BOOK"):
        icon += " fa-book";
        break;
      case ("MOVIE"):
        icon += " fa-file-movie-o";
        break;
      default:
        icon += " fa-link";
    }


    return (
      <>
        <div className="w3-col s3">
          <div className="w3-hover-shadow w3-display-container w3-white">
            <header className="w3-container">
              <h5><i className={icon}></i></h5>
            </header>
            <div className="w3-container">
              <p>{name}</p>
              <hr></hr>
              <p>{description}</p><br></br>
            </div>
            <div className="w3-display-container w3-row w3-hover-opacity-off" style={{ height: 35 + 'px', width: 200 + 'px' }}>
              <a href="#" className="w3-col w3-button w3-display-hover" style={{ width: 20 + '%' }}><i
                className="fa fa-edit"></i></a>
              <a href="#" onClick={(e) => onClickDelete(e,dispatch, id)} className="w3-col w3-button w3-display-hover" style={{ width: 20 + '%' }}><i
                className="fa fa-trash"></i></a>
              <a href="#" className="w3-col w3-button w3-display-hover" style={{ width: 20 + '%' }}><i
                className="fa fa-tags"></i></a>
              <a href="#" className="w3-col w3-button w3-display-hover" style={{ width: 20 + '%' }}><i
                className="fa fa-star-o"></i></a>
              <a href="#" className="w3-col w3-button w3-display-hover" style={{ width: 20 + '%' }}><i
                className="fa fa-archive"></i></a>
            </div>
          </div>
        </div>

      </>)
  }

  async function onClickDelete(event, dispatch, id) {
    event.stopPropagation();
    const t = await deleteActivity(id);
    dispatch({ type: Events.DELETED, id: id });
  }

