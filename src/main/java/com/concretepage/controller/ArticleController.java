package com.concretepage.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.concretepage.entity.Article;
import com.concretepage.service.IArticleService;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@Controller
@RequestMapping("user")
@EnableJms
public class ArticleController {
	@Autowired
	private IArticleService articleService;
	
	@Autowired
    private JmsTemplate jmsTemplate;
	
	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	private String rabbitExchange = "VasuExchange";
	
	private String rabbitRoutingKey = "VQ2";
	
	@Autowired
	private StringRedisTemplate template;
	
	@GetMapping("sendMessageToIbmMq")
	public ResponseEntity<String> sendMessageToIbmMq() {
		jmsTemplate.convertAndSend("DEV.QUEUE.1", "Hi, IbmMQ, How are you ?");
		return new ResponseEntity<String>("Message is sent to IBM MQ successfully...", HttpStatus.OK);
	}
	
	@GetMapping("sendMessageToRabbitMq")
	public ResponseEntity<String> sendMessageToRabbitMq() {
		rabbitTemplate.convertAndSend(rabbitExchange, rabbitRoutingKey, "Hi, RabbitMQ, How are you ?");
		return new ResponseEntity<String>("Message is sent to RabbitMQ successfully...", HttpStatus.OK);
	}
	
	@GetMapping("setValueInRedis")
	public ResponseEntity<String> setValueInRedis() {		
	    ValueOperations<String, String> ops = template.opsForValue();
		String key = "spring.boot.redis.test";
		if (!this.template.hasKey(key)) {
			ops.set(key, "Good morning, How are you ?");
		}
		System.out.println("Found key " + key + ", value=" + ops.get(key) + " in Redis");		
        return new ResponseEntity<String>("Value is set in Redis successfully...", HttpStatus.OK);		
	}
	
	@GetMapping("article/{id}")
	public ResponseEntity<Article> getArticleById(@PathVariable("id") Integer id) {
		Article article = articleService.getArticleById(id);
		return new ResponseEntity<Article>(article, HttpStatus.OK);
	}
	@GetMapping("articles")
	public ResponseEntity<List<Article>> getAllArticles() {
		List<Article> list = articleService.getAllArticles();
		return new ResponseEntity<List<Article>>(list, HttpStatus.OK);
	}
	@PostMapping("article")
	public ResponseEntity<Void> addArticle(@RequestBody Article article, UriComponentsBuilder builder) {
        boolean flag = articleService.addArticle(article);
        if (flag == false) {
        	return new ResponseEntity<Void>(HttpStatus.CONFLICT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/article/{id}").buildAndExpand(article.getArticleId()).toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}
	@PutMapping("article")
	public ResponseEntity<Article> updateArticle(@RequestBody Article article) {
		articleService.updateArticle(article);
		return new ResponseEntity<Article>(article, HttpStatus.OK);
	}
	@DeleteMapping("article/{id}")
	public ResponseEntity<Void> deleteArticle(@PathVariable("id") Integer id) {
		articleService.deleteArticle(id);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}	
} 