import React from "react";
import { addActivity } from "./ActivitiesService";
import { Events } from "./ActivitiesContext";
import { useActivitiesDispatch } from "./ActivitiesContext";

export default function ActivityForm({ toggleModal }) {
  let dispatch = useActivitiesDispatch();

  async function onSubmit(event) {
    event.preventDefault();
    event.stopPropagation();
    let entries = Object.fromEntries(new FormData(event.target).entries());

    let tags = entries.tags.split(";");

    let media = {
      name: "link",
      description: "link",
      type: "LINK",
      uri: entries.link,
    };

    let newobj = {
      name: entries.name,
      description: entries.description,
      type: entries.type,
      tags,
      media: [media],
    };
    let res = await addActivity(newobj);
    toggleModal(false);
    dispatch({ type: Events.ADDED, data: res.data });
  }

  return (
    <>
      <form
        className="w3-container w3-card-4 w3-light-grey"
        onSubmit={(e) => onSubmit(e)}
      >
        <h6>
          <i className="fa fa-address-book-o"> </i>
        </h6>

        <p>
          <label>Type</label>
          <select
            className="w3-select w3-border"
            name="type"
            defaultValue="option"
          >
            <option value="" disabled>
              Choose your option
            </option>
            <option value="LINK">Web Site</option>
            <option value="MOVIE">Movie</option>
            <option value="BOOK">Book</option>
            <option value="BLOG">Blog</option>
            <option value="OTHER">Other</option>
          </select>
        </p>
        <p>
          <label>Name</label>
          <input className="w3-input w3-border" name="name" type="text" />
        </p>
        <p>
          <label>Description</label>
          <input
            className="w3-input w3-border"
            name="description"
            type="text"
          />
        </p>
        <p>
          <label>Link</label>
          <input className="w3-input w3-border" name="link" type="text" />
        </p>
        <p>
          <label>Tags</label>
          <input className="w3-input w3-border" name="tags" type="text" />
        </p>
        <p>
          <button className="w3-btn w3-dark-grey">Add</button>
        </p>
      </form>
    </>
  );
}
