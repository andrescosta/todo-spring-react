import React, { createContext, useContext, useReducer, useEffect } from "react";
import { getActivities } from "./ActivitiesService";

const ActivitiesContext = createContext(null);

const ActivitiesDispatchContext = createContext(null);

export function ActivitiesProvider({ children }) {
  const [activities, dispatch] = useData();

  return (
    <ActivitiesContext.Provider value={activities}>
      <ActivitiesDispatchContext.Provider value={dispatch}>
        {children}
      </ActivitiesDispatchContext.Provider>
    </ActivitiesContext.Provider>
  );
}

export function useActivities() {
  return useContext(ActivitiesContext);
}

export function useActivitiesDispatch() {
  return useContext(ActivitiesDispatchContext);
}

export const Events = {
  LOADED: Symbol("loaded"),
  ADDED: Symbol("added"),
  UPDATED: Symbol("updated"),
  DELETED: Symbol("deleted"),
  FILTEREDBYTYPE: Symbol("filteredbytype"),
  FILTERRESETED: Symbol("notfiltered"),
};

function activitiesReducer(activities, action) {
  switch (action.type) {
    case Events.LOADED: {
      return { ok: action.result.ok, data: [...action.result.data] };
    }
    case Events.ADDED: {
      let ret = {
        ok: true,
        data: [...activities.data, action.data],
      };
      console.log(ret);
      return ret;
    }
    case Events.UPDATED: {
      return activities.map((t) => {
        if (t.id === action.task.id) {
          return action.task;
        } else {
          return t;
        }
      });
    }
    case Events.DELETED: {
      return {
        ok: true,
        data: activities.data.filter((t) => t.publicId !== action.id),
      };
    }
    case Events.FILTEREDBYTYPE: {
      return {
        ok: true,
        data: activities.data.map((obj) => ({
          ...obj,
          show: obj.type === action.typeActivity,
        })),
      };
    }
    case Events.FILTERRESETED: {
      return {
        ok: true,
        data: activities.data.map((obj) => ({ ...obj, show: true })),
      };
    }
    default: {
      throw Error("Unknown action: " + action.type);
    }
  }
}

function useData() {
  const [activities, dispatch] = useReducer(activitiesReducer, {
    ok: true,
    data: [],
  });
  useEffect(() => {
    async function fetchData() {
      let ignore = false;
      if (!ignore) {
        const data = await getActivities();
        dispatch({ type: Events.LOADED, result: data });
      }
      return () => {
        ignore = true;
      };
    }
    fetchData();
  }, []);
  return [activities, dispatch];
}
