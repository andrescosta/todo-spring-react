import axios from "axios";

const svc = "http://localhost:8080/";

export async function get({ api, pathParams, queryParams }) {
  try {
    let params = "";
    if (pathParams) {
      pathParams.map((p) => (params += "/" + p));
    }
    const url = svc + api + params;
    const response = await axios.get(url, { params: queryParams });
    return { ok: true, data: response.data };
  } catch (e) {
    console.log("Error:" + e);
    return { ok: false, data: [] };
  }
}

export async function ddelete({ api, parameters }) {
  try {
    let params = "";
    parameters.map((p) => (params += "/" + p));
    const url = svc + api + params;
    const response = await axios.delete(url);
    return { ok: true };
  } catch (e) {
    console.log("Error:" + e);
    return { ok: false, data: [] };
  }
}

export async function post({ api, parameters, data }) {
  try {
    let params = "";
    if (parameters) {
      parameters.map((p) => (params += "/" + p));
    }
    const url = svc + api + params;
    const response = await axios.post(url, data);
    return { ok: true, data: response.data };
  } catch (e) {
    console.log("Error:" + e);
    return { ok: false, data: [] };
  }
}
