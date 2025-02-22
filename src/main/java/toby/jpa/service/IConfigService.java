package toby.jpa.service;

import toby.jpa.dto.ConfigDto;

import java.util.List;

public interface IConfigService {

    List<ConfigDto> listAllConfig();
    List<ConfigDto> listGuildConfig(String guildId);
    ConfigDto getConfigByName(String name, String guildId);
    ConfigDto createNewConfig(ConfigDto configDto);
    ConfigDto updateConfig(ConfigDto configDto);
    void deleteAll(String guildId);

}
