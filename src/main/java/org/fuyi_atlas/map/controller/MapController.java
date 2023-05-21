package org.fuyi_atlas.map.controller;

import org.fuyi_atlas.map.service.DefaultMapService;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2023/3/30 15:03
 * @since: 1.0
 **/
@RestController
@RequestMapping("/map")
public class MapController {

    @Bean
    public FilterRegistrationBean corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // config.addAllowedOrigin("*");
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    @Autowired
    private DefaultMapService mapService;

    @RequestMapping(value = "/{z}/{x}/{y}", produces = {"application/vnd.mapbox-vector-tile"})
    public ResponseEntity<byte[]> fetchTile(@PathVariable("z")Integer z, @PathVariable("x")Integer x, @PathVariable("y")Integer y, HttpServletResponse response) throws FactoryException, TransformException, IOException {
        return ResponseEntity.ok(mapService.generate(x, y, z));
    }

    @GetMapping("/test")
    public String test(){
        return "success";
    }
}
