import axios from "axios";

const url = "http://localhost:8085";

const interceptorInstance = axios.create({
    baseURL:url
});

interceptorInstance.interceptors.request.use(
    (config) =>{
        const jwtToken = localStorage.getItem("accessToken");
        if(config.url === `${url}/api/auth/login`){
            localStorage.clear();
            return config;
        }
        
        if(jwtToken){
            config.headers["Authorization"]=`Bearer ${jwtToken}`;
            config.headers["Content-Type"] ="application/json";
        }
        return config;

    },
    (error)=>{
        console.log("error",error);
        return Promise.reject(error);
        
    }
)


interceptorInstance.interceptors.response.use(
    (response)=>{
        return response;
    },
    (error)=>{
        if(error.response){
            if(error.response.status === 401){
                console.error("Unauthorized: Please log in again");      
            } else if(error.response.status === 404){
                console.error("Not Found: The request resource was not found");   
            } else{
                console.error("An error occurred: ",error.response.data);
                
            }
        } else{
            console.error("Network error: ",console.message);
            
        }
        return Promise.reject(error);
    }
)

export default interceptorInstance;