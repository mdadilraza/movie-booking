import { loginUrl ,registerUrl} from "../services/api";
import { createSlice,createAsyncThunk, } from "@reduxjs/toolkit";

const initialState = {
    user:null,
    token:null,
    loading:false,
    error:null
}

export const login =createAsyncThunk('auth/login',async(user,{rejectWithValue})=>{
    try{
     const response = await loginUrl(user);
     console.log("resposne",response.data);
     localStorage.setItem("accessToken",response.data.result[0]?.accessToken);
     localStorage.setItem("refreshToken",response.data.result[0]?.refreshToken);
     localStorage.setItem("role",response.data.result[0]?.userDetails.role);
     return response.data;
     
    }catch(error){
      return rejectWithValue(error.response.data);
    }
        
})

export const register = createAsyncThunk('auth/register',async(user,{rejectWithValue})=>{
  try{
    const response = await registerUrl(user);
    console.log("register",response.data);
    
    return response.data;
  }catch(error){
    console.log("error",error);
    
    return rejectWithValue(error.response.data);
  }
})

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout: (state) => {
      state.user = null;
      state.token = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        state.token = action.payload.token; 
       
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Something went wrong'; 
      })
      .addCase(register.pending,(state)=>{
        state.loading = true;
        state.error = null;
      })
      .addCase(register.fulfilled,(state,action)=>{
        state.loading=false;
        state.user=action.payload.user;
      })
      .addCase(register.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Something went wrong'; 
      })
      
  },
});


export const { logout } = authSlice.actions;
export default authSlice.reducer;