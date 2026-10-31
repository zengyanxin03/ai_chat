package com.agent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 可提高大型语言模型的推理能力
 */
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

	private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
			{re2_input_query}
			Read the question again: {re2_input_query}
			""";

	private final String re2AdviseTemplate;


	public ReReadingAdvisor() {
		this(DEFAULT_RE2_ADVISE_TEMPLATE);
	}

	public ReReadingAdvisor(String re2AdviseTemplate) {
		this.re2AdviseTemplate = re2AdviseTemplate;
	}

	/**
	 * 执行请求前,改写Prompt
	 * @param chatClientRequest
	 * @return
	 */
	public ChatClientRequest before(ChatClientRequest chatClientRequest) {
		String augmentedUserText = PromptTemplate.builder()
			.template(this.re2AdviseTemplate)
			.variables(Map.of("re2_input_query", chatClientRequest.prompt().getUserMessage().getText()))
			.build()
			.render();

		return chatClientRequest.mutate()
			.prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
			.build();
	}

	@Override
	public int getOrder() { 
		return 0;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		return callAdvisorChain.nextCall(this.before(chatClientRequest));
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
		return streamAdvisorChain.nextStream(this.before(chatClientRequest));
	}

	@Override
	public String getName() {
		return "";
	}
}
