package com.example.mq.messaginglibrary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("savingsclosingbalancequeryresponse")
public class SavingsBalanceResponseModel extends ResponseBaseModel {

}
