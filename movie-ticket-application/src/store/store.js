import { configureStore } from "@reduxjs/toolkit";
import authReducer from '../slice/UserSlice';

export const store = configureStore({
    reducer:{
        auth:authReducer,
    }
})