package org.jy.jamye.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig(
    var octetStreamReadMsgConverter: OctetStreamReadMsgConverter,
) : WebMvcConfigurer {


    @Autowired
    fun WebConfig(octetStreamReadMsgConverter: OctetStreamReadMsgConverter) {
        this.octetStreamReadMsgConverter = octetStreamReadMsgConverter;
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(octetStreamReadMsgConverter);
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.HEAD.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name()
            )
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600) //pre-flight 설정
    }
}

@Component
class OctetStreamReadMsgConverter @Autowired constructor(objectMapper: ObjectMapper?) :
    AbstractJackson2HttpMessageConverter(objectMapper!!, MediaType.APPLICATION_OCTET_STREAM) {

    override fun canWrite(mediaType: MediaType?): Boolean {
        return false
    }
}