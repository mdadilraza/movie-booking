import interceptorInstance from "./interceptor";

const baseURL = "http://localhost:8085";

const api = (method, endpoint, data) =>
    interceptorInstance[method](`${baseURL}${endpoint}`, data);

export const loginUrl = (user) => api("post","/api/auth/login",user);

export const registerUrl = (user) => api("post","/api/auth/register",user);