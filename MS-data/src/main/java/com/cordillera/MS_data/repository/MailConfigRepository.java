package com.cordillera.MS_data.repository;

import com.cordillera.MS_data.entity.MailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailConfigRepository extends JpaRepository<MailConfig, Long> {
}
