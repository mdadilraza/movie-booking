import React, { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { login } from '../slice/UserSlice';
import { Button, Card, TextField } from '@mui/material';

const UserLogin = () => {
  const cardStyles = {
    width: '300px',
    height: 'auto',
    display: 'flex',
    flexDirection: 'column',
    padding: '30px',
 
  };
  const dispatch = useDispatch();
  const { loading, error } = useSelector((state) => state.auth);
  const [formError, setFormError] = useState(""); 
  const [formData, setFormData] = useState({
      username: "",
      password: "",
    }); 

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
      };
    
      const handleSubmit = (e) => {
        e.preventDefault();
    
       
        if (!formData.username || !formData.password) {
          setFormError("All fields are required!");
          return;
        }
    
        setFormError("");  
        dispatch(login(formData)) 
          .unwrap()  
          .then(() => {
            console.log("Login successful", formData);  
           
          })
          .catch((error) => {
            setFormError(error.message || "login failed");  
          });
      };
  return (
    <>
    <form onSubmit={handleSubmit} style={cardStyles}>
     
        <h2>Login</h2>
        {formError && <p style={{ color: 'red' }}>{formError}</p>}
        {error && <p style={{ color: 'red' }}>{error}</p>}

        <TextField
          label="Username"
          name="username"
          variant="standard"
          value={formData.username}
          size="small"
          onChange={handleChange}
          />
        <TextField
          label="Password"
          name="password"
          type="password"
          variant="standard"
          value={formData.password}
          size="small"
          onChange={handleChange}
          />
        <Button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </Button>
     
    </form>
          </>
  )
}

export default UserLogin