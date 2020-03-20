package cn.fantasticmao.dnshooks.log2es.service;

import cn.fantasticmao.dnshooks.log2es.dto.Message;

import java.io.IOException;

/**
 * MessageService
 *
 * @author maomao
 * @since 2020-03-21
 */
public interface MessageService extends AutoCloseable {

    boolean createIfNotExists() throws IOException;

    boolean save(Message message) throws IOException;
}
