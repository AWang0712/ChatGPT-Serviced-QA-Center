package com.allanwang.chatbot.api.application.job;

import com.allanwang.chatbot.api.domain.ai.IOpenAI;
import com.allanwang.chatbot.api.domain.zsxq.IZsxqApi;
import com.allanwang.chatbot.api.domain.zsxq.model.aggregates.UnAnsweredQuestionsAggregates;
import com.allanwang.chatbot.api.domain.zsxq.model.vo.Topics;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class ChatbotTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ChatbotTask.class);

    private String groupName;

    private String groupId;

    private String cookie;
    private String openAiKey;
    private boolean silenced;

    private IZsxqApi zsxqApi;
    private IOpenAI openAI;

    public ChatbotTask(String groupName, String groupId, String cookie, String openAiKey, IZsxqApi zsxqApi, IOpenAI openAI, boolean silenced) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.cookie = cookie;
        this.openAiKey = openAiKey;
        this.zsxqApi = zsxqApi;
        this.openAI = openAI;
        this.silenced = silenced;
    }

    @Override
    public void run() {
        try {
            if (new Random().nextBoolean()) {
                logger.info("{} random idle", groupName);
                return;
            }

            GregorianCalendar calendar = new GregorianCalendar();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour > 1 && hour < 8) {
                logger.info("{} Not available!", groupName);
                return;
            }

            // 1. Search question
            UnAnsweredQuestionsAggregates unAnsweredQuestionsAggregates = zsxqApi.queryUnAnsweredQuestionsTopicId(groupId, cookie);
            logger.info("{} query result：{}", groupName, JSON.toJSONString(unAnsweredQuestionsAggregates));
            List<Topics> topics = unAnsweredQuestionsAggregates.getResp_data().getTopics();
            if (null == topics || topics.isEmpty()) {
                logger.info("{} idle, answer later", groupName);
                return;
            }

            // 2. AI answer
            Topics topic = topics.get(topics.size() - 1);
            String answer = openAI.doChatGPT(openAiKey, topic.getQuestion().getText().trim());

            // 3. Answer question
            boolean status = zsxqApi.answer(groupId, cookie, topic.getTopic_id(), answer, silenced);
            logger.info("{} N0.：{} Question：{} Answer：{} Status：{}", groupName, topic.getTopic_id(), topic.getQuestion().getText(), answer, status);
        } catch (Exception e) {
            logger.error("{} Error", groupName, e);
        }
    }

}
