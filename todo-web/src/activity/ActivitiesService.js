import { get, ddelete, post } from '../common/RestUtil'


export async function getActivities() {
    const response = await get({api: "v1/activities"})
    response.data.map(p => p.show = true);
    return response;
}

export async function addActivity(activity) {

    const response = await post({
        api: "v1/activities",
        data: activity
    });
    response.data.show = true;
    return response;

}

export function updateActivity(activity) {

}

export async function deleteActivity(id) {
    const response = await ddelete({
        api: "v1/activities",
        parameters: [id]
    })
    return response;

}