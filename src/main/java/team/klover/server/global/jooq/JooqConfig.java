package team.klover.server.global.jooq;


import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.geolatte.geom.M;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultDSLContext;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.modelmapper.module.jdk8.Jdk8Module;
import org.modelmapper.module.jsr310.Jsr310Module;
import org.modelmapper.spi.NameTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {
    @Bean
    public DefaultDSLContext dslContext(org.jooq.Configuration config){
        config.set(SQLDialect.POSTGRES);
        config.set(new Settings().withExecuteLogging(true));
        return new DefaultDSLContext(config);
    }

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);

        return mapper;
    }
}
