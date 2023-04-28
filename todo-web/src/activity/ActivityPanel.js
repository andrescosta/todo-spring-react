import ActivityCard from "./ActivityCard";
import { useActivities } from "./ActivitiesContext";
import React from "react";

export default function ActivityPanel() {
    const result = useActivities();
    if (result.ok) {
      const data = result.data;
      const panels = []
      let items = []

      let acs = data.filter(p => p.show);
      acs.map((activity, index) => {
        items.push(<ActivityCard key={activity.publicId} type={activity.type} name={activity.name} description={activity.description} id={activity.publicId} />)
        if ((index + 1) % 4 == 0) {
          panels.push(<div key={index} className="w3-row-padding"
            style={{ width: 100 + '%', marginTop: 43 + 'px' }}>{items}</div>)
          items = []
        }
      })
      if (items.length > 0) {
        panels.push(<div key={data.length} className="w3-row-padding"
          style={{ width: 100 + '%', marginTop: 43 + 'px' }}>{items}</div>)
      }
      return (
        <>
          <div className="w3-panel">
            {panels}
          </div>
        </>
      );
    } else {
      return (
        <>
          <div className="w3-panel">
            <div className="w3-panel w3-pale-red w3-border">
              <h3>OOPs</h3>
              <p>We cannot get the information. Please try again later</p>
            </div>
          </div>
        </>
      );

    }
  }
