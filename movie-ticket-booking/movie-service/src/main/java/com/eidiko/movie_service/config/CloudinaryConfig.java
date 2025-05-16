package com.eidiko.movie_service.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary (){
        Dotenv dotenv = Dotenv.load();
        return new Cloudinary(dotenv.get("CLOUDINARY_URL"));
//        return new Cloudinary(ObjectUtils.asMap(
//                "cloud_name",cloudinary.config.cloudName,
//                "api_key" ,cloudinary.config.apiKey ,
//                "api_secret",cloudinary.config.apiSecret
//        ));
    }
}
