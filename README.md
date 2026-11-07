# ai_chat
企业法务咨询助手

##### RAG检索增强生成工作流程

<img width="1430" height="824" alt="image" src="https://github.com/user-attachments/assets/97c3f249-74ca-4fb6-b50b-a910c50d251e" />


##### 工具调用流程

1.用户提出"请用一句话介绍什么是劳动法?"

2.程序将问题传递给大模型

3.大模型分析问题，判断需要使用工具(网页检索工具)来获取信息

4.大模型输出工具名称和参数

5.程序接收工具调用请求，执行网页抓取操作

6.工具执行抓取并返回数据

7.程序将抓取的数据传回给大模型

8.大模型分析网页数据，生成关于"什么是劳动法?"的回答

9.程序将大模型的回答输出给用户

<img width="1490" height="838" alt="image" src="https://github.com/user-attachments/assets/da0a3bcb-e73b-43ec-95ff-f7295870a43e" />

<img width="2512" height="1306" alt="image" src="https://github.com/user-attachments/assets/33a95f5f-d94f-47c8-9367-6251aa37328a" />

##### ReAct 模式

1.推理（Reason）:将原始问题拆分为多步骤任务，明确当前要执行的步骤，比如 “第一步需要打开xx网站”。

2.行动（Act）：调用外部工具执行动作，比如调用搜索引擎、打开浏览器访问网页等。

3.观察（Observe）：获取工具返回的结果，反馈给智能体进行下一步决策。比如将打开的网页代码输入给 AI。

4.循环迭代：不断重复上述 3 个过程，直到任务完成或达到终止条件。
<img width="1214" height="616" alt="image" src="https://github.com/user-attachments/assets/f26507ed-dcca-4ed0-81b7-6a3b4e1e6858" />

