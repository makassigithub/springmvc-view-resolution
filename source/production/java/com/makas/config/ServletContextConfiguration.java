package com.makas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author Brahima This is how spring resolves views: when Controller method
 *         returns: 1.implementation of View or ModelAndView with an
 *         implementation of View passed to the ModelAndView constructor, spring
 *         uses that View directly. 2.A string view name or ModelAndView
 *         constructed with a String view name, Spring uses
 *         org.springframework.web.servlet.ViewResolver 3.Model, ModelMap (+-
 *         ModelAttribute), Spring implicitly translate the request into a view
 *         name using the configured RequestToViewNameTtranslator, and resolve
 *         the view using ViewResolver 4.A ResponseEntity || HttpEntity (case
 *         xml, json) Spring uses a content Negotiation
 * 
 *         NB: 1. If a return type is any object, spring assumes the that object
 *         should be an attribute in the model it uses the camelCase name of the
 *         Object class name to set the object attribute in the model(otherwise
 * @modelAttribute is specified in the method decoration), before using the
 *                 RequestToViewNameTtranslator 2. A return type can be
 *                 java.util.concurent.Callable<?> ||
 *                 org.springframework.web.context.request.async.defferedResult<?>
 *                 in case response may be delayed. the return type in method
 *                 signature can any previous define types (View,ModelAndView,
 *                 Model,ModelMap, ResponseEntity String) and it can be
 *                 decorated
 *                 with @ResponseBody, @ResponseStatus, @ModelAttribute.
 * 
 *                 * Here is how Spring uses content negotiation 1. It first
 *                 looks for the file extension on the request (.html, .xml,
 *                 .json and so on) to determine the request format. If the ext.
 *                 is not there or not recongnizable,it mooves on 2. It use a
 *                 request param named 'format', is present the values will be
 *                 .html, .xml, .json and so on. that value is use to define
 *                 request content-type. the 'format' param can be renamed (seee
 *                 configureContentNegotiation()) 3. It finally uses the
 *                 'accept' parameter to determine the wanted response format.
 **/

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.makas.site", useDefaultFilters = false, includeFilters = @ComponentScan.Filter(Controller.class))
public class ServletContextConfiguration extends WebMvcConfigurerAdapter {

	// use CDI to inject ObjectMapper and Marshallers
	@Inject
	ObjectMapper objectMapper;
	@Inject
	Marshaller marshaller;
	@Inject
	Unmarshaller unmarshaller;

	// Override this method to configure custom messageConverters
	// We rarely need this
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		// add this basic converters
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		converters.add(new SourceHttpMessageConverter<>());

		// Create your xml converter, set marshallers and add it to the converters
		MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();
		xmlConverter.setSupportedMediaTypes(
				Arrays.asList(new MediaType("application", "xml"), new MediaType("text", "xml")));
		xmlConverter.setMarshaller(this.marshaller);
		xmlConverter.setUnmarshaller(this.unmarshaller);
		converters.add(xmlConverter);

		// Json converter needs an object mapper , add it to conveter
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setSupportedMediaTypes(
				Arrays.asList(new MediaType("application", "json"), new MediaType("text", "json")));
		jsonConverter.setObjectMapper(this.objectMapper);
		converters.add(jsonConverter);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(true)
		          .favorParameter(false)
		          .parameterName("mediaType").ignoreAcceptHeader(false)
				  .useJaf(false)
				  .defaultContentType(MediaType.APPLICATION_XML)
				  .mediaType("xml", MediaType.APPLICATION_XML)
				  .mediaType("json", MediaType.APPLICATION_JSON);
	}

	// Spring uses a view resolver when a controller handler method
	// resolve a view from a String
	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/jsp/view/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	public RequestToViewNameTranslator viewNameTranslator() {
		return new DefaultRequestToViewNameTranslator();
	}
}
