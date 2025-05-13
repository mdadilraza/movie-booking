import React, { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { login } from '../slice/UserSlice';


const UserLogin = () => {
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
    <div className="login-form">
    <h2>login</h2>
    <form onSubmit={handleSubmit}>
     
      <div>
        <label htmlFor="text">username:</label>
        <input
          type="text"
          id="text"
          name="username"
          value={formData.username}
          onChange={handleChange}
        />
      </div>
    
      <div>
        <label htmlFor="password">Password:</label>
        <input
          type="password"
          id="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
        />
      </div>
      {formError && <p style={{ color: "red" }}>{formError}</p>}  
      {error && <p style={{ color: "red" }}>{error.message || "login failed"}</p>} 
      <div>
        <button type="submit" disabled={loading}>
          {loading ? "login..." : "login"}
        </button>
      </div>
    </form>
  </div>
  )
}

export default UserLogin