package com.Dotsquares.BoilerPlateAuth.Repository;

import com.Dotsquares.BoilerPlateAuth.Entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log,Long> {
}
