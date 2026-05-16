package com.hireconnect.profileservice.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hireconnect.profileservice.entity.SkillDictionary;
import com.hireconnect.profileservice.repository.SkillDictionaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillDictionarySeeder implements CommandLineRunner {

    private final SkillDictionaryRepository skillDictionaryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (skillDictionaryRepository.count() == 0) {
            log.info("Seeding Skill Dictionary with default tech skills...");
            
            List<String> baseSkills = Arrays.asList(
                    "Java", "Spring Boot", "Spring", "Hibernate", "JPA", "Microservices",
                    "Python", "Django", "Flask", "FastAPI",
                    "JavaScript", "TypeScript", "Node.js", "Express", "React", "Angular", "Vue.js", "Next.js",
                    "C#", ".NET", "ASP.NET", "C++", "C", "Go", "Golang", "Rust", "Ruby", "Ruby on Rails",
                    "PHP", "Laravel", "Swift", "Kotlin", "Android", "iOS",
                    "Docker", "Kubernetes", "AWS", "Amazon Web Services", "Azure", "GCP", "Google Cloud",
                    "CI/CD", "Jenkins", "Git", "GitHub", "GitLab", "Terraform", "Ansible",
                    "MySQL", "PostgreSQL", "Oracle", "SQL Server", "MongoDB", "Redis", "Cassandra", "Elasticsearch",
                    "GraphQL", "REST API", "gRPC", "Kafka", "RabbitMQ", "ActiveMQ",
                    "HTML", "CSS", "SASS", "Tailwind CSS", "Bootstrap",
                    "Machine Learning", "Data Science", "Artificial Intelligence", "Deep Learning", "TensorFlow", "PyTorch",
                    "Linux", "Unix", "Bash", "Shell Scripting", "Agile", "Scrum"
            );

            for (String skillName : baseSkills) {
                if (skillDictionaryRepository.findBySkillNameIgnoreCase(skillName).isEmpty()) {
                    skillDictionaryRepository.save(SkillDictionary.builder()
                            .skillName(skillName)
                            .category("General Tech")
                            .build());
                }
            }
            log.info("Skill Dictionary seeded successfully.");
        }
    }
}
