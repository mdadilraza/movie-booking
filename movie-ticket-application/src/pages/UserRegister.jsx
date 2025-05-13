import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { register } from '../slice/UserSlice'; 

const UserRegister = () => {
  const dispatch = useDispatch();
  const { loading, error } = useSelector((state) => state.auth); 
  const [formError, setFormError] = useState(""); 
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
    fullName: "",
    phoneNumber: "",

  });

  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();

   
    if (!formData.username || !formData.password || !formData.email || !formData.phoneNumber || !formData.fullName) {
      setFormError("All fields are required!");
      return;
    }

    setFormError("");  
    dispatch(register(formData)) 
      .unwrap()  
      .then(() => {
        console.log("Registration successful", formData);  
       
      })
      .catch((error) => {
        setFormError(error.message || "Registration failed");  
      });
  };

  return (
    <div className="registration-form">
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="firstname">First Name:</label>
          <input
            type="text"
            id="username"
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
        <div>
          <label htmlFor="email">Email:</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
          />
        </div>
        <div>
          <label htmlFor="password">fullName:</label>
          <input
            type="text"
            id="fullanme"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
          />
        </div>
        <div>
          <label htmlFor="phoneNumber">Phone Number:</label>
          <input
            type="text"
            id="phoneNumber"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={handleChange}
          />
        </div>
        
        {formError && <p style={{ color: "red" }}>{formError}</p>}  
        {error && <p style={{ color: "red" }}>{error.message || "Registration failed"}</p>} 
        <div>
          <button type="submit" disabled={loading}>
            {loading ? "Registering..." : "Register"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default UserRegister;