import { get, ddelete, post } from './RestUtil'


export async function getActivities() {
    const response = await get({api: "activities"})
    response.data.map(p => p.show = true);
    return response;
}

export async function getActivitiesByType(type) {
    const response = await get({
        api: "activities/users",
        pathParams: ["d9630fbe-fa4b-4c32-9187-3f254d822943"],
        queryParams: [type]
    })
    return response;
}

export async function addActivity(activity) {

    const response = await post({
        api: "activities",
        data: activity
    });
    response.data.show = true;
    return response;

}

export function updateActivity(activity) {

}

export async function deleteActivity(id) {
    const response = await ddelete({
        api: "activities",
        parameters: [id]
    })
    return response;

}