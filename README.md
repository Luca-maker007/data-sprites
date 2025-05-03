# Lingxi Intelligence AIGC-BI Platform

## Project Introduction
The Lingxi Intelligence AIGC-BI Platform is a smart data analysis platform built on the Spring Boot, Message Queue (MQ), Artificial Intelligence Generated Content (AIGC), and React technology stack. Compared to traditional Business Intelligence (BI) tools, this platform allows users to simply import raw datasets and input their analysis requirements to automatically generate visual charts and analytical conclusions, significantly reducing the cost and increasing the efficiency of data analysis.

![image](https://github.com/user-attachments/assets/8b649e01-824a-42b9-a83d-81b1d87d116a)


## Core Features
1. **Data Import and Management**:
   - Supports importing various data source formats (e.g., CSV, Excel, JSON).
   - Provides data preprocessing capabilities, including data cleaning, format conversion, and data merging.
   - Offers version management for datasets, enabling users to trace and compare different versions of data.

![image](https://github.com/user-attachments/assets/ef559f65-281a-4b45-ae39-20402b59354f)

2. **Smart Analysis Engine**:
   - Users can input simple analysis requests (e.g., "Analyze sales trends," "Identify high-value customers"), and the platform will automatically invoke AIGC models to generate precise analytical conclusions.
   - Combines machine learning algorithms to deeply mine data, uncovering hidden trends, associations, and anomalies within the data.

3. **Visualization Chart Generation**:
   - Automatically generates various types of visual charts (e.g., bar charts, line charts, pie charts, heatmaps) based on analysis results.
   - Provides a rich set of chart styles and customization options, allowing users to adjust colors, layouts, and interactivity according to their needs.

4. **Report Generation and Sharing**:
   - Automatically generates analysis reports containing conclusions and visual charts, supporting export in multiple formats (e.g., PDF, HTML, Word).
   - Supports sharing reports with team members or clients, with options for online viewing and offline download.

5. **Real-time Data Monitoring and Alerts**:
   - Supports real-time monitoring of key indicators, with users able to set alert rules to trigger notifications (e.g., email, SMS, instant messaging) when data reaches predefined thresholds.
   - Provides data dashboard functionality to display dynamic changes in key indicators in real-time.

## Technical Architecture
- **Frontend**: React + Ant Design
  - Built with the React framework to provide a smooth user interface and experience.
  - Combined with Ant Design component library to achieve aesthetically pleasing and efficient interface design.
- **Backend**: Spring Boot + RabbitMQ
  - Developed with the Spring Boot framework to provide robust data processing and business logic support.
  - Utilizes RabbitMQ for asynchronous message queuing to enhance system performance and response speed.
- **Data Analysis Engine**: AIGC + Data Mining Algorithms
  - Integrates advanced AIGC models to achieve natural language understanding and generation, providing users with intelligent analysis recommendations.
  - Combines traditional data mining algorithms to deeply mine data value, ensuring the accuracy and reliability of analysis results.

## Use Cases
1. **Enterprise Data Analysis**:
   - Financial departments can quickly analyze financial statements to generate reports on key indicators such as profit, costs, and cash flow.
   - Sales teams can monitor sales data in real-time, analyze sales trends, predict market demand, and develop precise sales strategies.
2. **Market Research**:
   - Market researchers can import market research data to quickly generate market analysis reports, gaining insights into consumer needs and market trends.
   - Marketing personnel can analyze advertising effectiveness, optimize advertising strategies, and improve marketing ROI.
3. **Customer Relationship Management**:
   - Customer service teams can analyze customer feedback data to quickly identify customer pain points and optimize customer service processes.
   - Customer relationship managers can analyze customer behavior data to identify high-value customers and develop customer loyalty programs.

## Installation and Deployment
### Environment Requirements
- **Operating System**: Linux or macOS
- **Java**: JDK 1.8 or higher
- **Node.js**: v14 or higher
- **Database**: MySQL 5.7 or higher
- **Message Queue**: RabbitMQ 3.8 or higher

### Installation Steps
1. **Clone the Project Code**:
   ```bash
   git clone https://github.com/your-repo/lingxi-bi-platform.git
   cd lingxi-bi-platform
   ```
2. **Install Backend Dependencies**:
   ```bash
   cd backend
   ./mvnw clean install
   ```
3. **Install Frontend Dependencies**:
   ```bash
   cd ../frontend
   npm install
   ```
4. **Configure the Database**:
   - Modify the `backend/src/main/resources/application.yml` file to configure database connection details.
   - Run the database initialization script located in the `backend/sql` directory.
5. **Start the Backend Service**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
6. **Start the Frontend Service**:
   ```bash
   cd ../frontend
   npm start
   ```
7. **Access the Platform**:
   - Open your browser and visit [http://localhost:3000](http://localhost:3000).

## Contribution Guidelines
We welcome all developers to contribute code, documentation, or suggestions for improvement to the Lingxi Intelligence AIGC-BI Platform. If you would like to participate in the development of this project, please follow these steps:
1. **Fork the Project**: Fork this project to your personal repository on GitHub.
2. **Create a Branch**: In your forked repository, create a new branch for developing your feature or fixing an issue.
3. **Commit Code**: Develop on your branch and commit your code.
4. **Initiate a Pull Request**: After completing your development, initiate a Pull Request to the `main` branch of this project.
5. **Wait for Review**: Project maintainers will review your code and may provide feedback for necessary modifications.

## Contact Information
- **Project Homepage**: [https://github.com/your-repo/lingxi-bi-platform](https://github.com/your-repo/lingxi-bi-platform)
- **Technical Support Email**: support@lingxi-bi.com
- **Developer Community**: Join our Slack group to exchange experiences with other developers.

## Copyright Notice
The Lingxi Intelligence AIGC-BI Platform is licensed under the [MIT License](https://opensource.org/licenses/MIT). You are free to use, modify, and distribute the code of this project.
