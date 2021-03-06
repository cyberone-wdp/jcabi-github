/**
 * Copyright (c) 2013-2014, JCabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.github.mock;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.github.Commit;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Pull;
import com.jcabi.github.PullComments;
import com.jcabi.github.Repo;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Mock Github pull.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.5
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString
@EqualsAndHashCode(of = { "storage", "self", "coords", "num" })
@SuppressWarnings("PMD.TooManyMethods")
final class MkPull implements Pull {

    /**
     * Storage.
     */
    private final transient MkStorage storage;

    /**
     * Login of the user logged in.
     */
    private final transient String self;

    /**
     * Repo name.
     */
    private final transient Coordinates coords;

    /**
     * Pull number.
     */
    private final transient int num;

    /**
     * Public ctor.
     * @param stg Storage
     * @param login User to login
     * @param rep Repo
     * @param number Pull request number
     * @checkstyle ParameterNumber (5 lines)
     */
    MkPull(
        @NotNull(message = "stg is never NULL") final MkStorage stg,
        @NotNull(message = "login is never NULL") final String login,
        @NotNull(message = "rep is never NULL") final Coordinates rep,
        final int number) {
        this.storage = stg;
        this.self = login;
        this.coords = rep;
        this.num = number;
    }

    @Override
    @NotNull(message = "repository is never NULL")
    public Repo repo() {
        return new MkRepo(this.storage, this.self, this.coords);
    }

    @Override
    public int number() {
        return this.num;
    }

    @Override
    @NotNull(message = "Iterable of commits is never NULL")
    public Iterable<Commit> commits() throws IOException {
        return Collections.emptyList();
    }

    @Override
    @NotNull(message = "Iterable of files is never NULL")
    public Iterable<JsonObject> files() throws IOException {
        return Collections.emptyList();
    }

    @Override
    public void merge(
        @NotNull(message = "msg can't be NULL") final String msg
    ) throws IOException {
        // nothing to do here
    }

    @Override
    @NotNull(message = "comments is never NULL")
    public PullComments comments() throws IOException {
        return new MkPullComments(this.storage, this.self, this.coords, this);
    }

    @Override
    public int compareTo(
        @NotNull(message = "pull cannot be NULL") final Pull pull
    ) {
        return this.number() - pull.number();
    }

    @Override
    public void patch(
        @NotNull(message = "json can't be NULL") final JsonObject json
    ) throws IOException {
        new JsonPatch(this.storage).patch(this.xpath(), json);
    }

    @Override
    @NotNull(message = "JSON is never NULL")
    public JsonObject json() throws IOException {
        final JsonObject obj = new JsonNode(
            this.storage.xml().nodes(this.xpath()).get(0)
        ).json();
        final JsonObjectBuilder json = Json.createObjectBuilder();
        for (final Map.Entry<String, JsonValue> val : obj.entrySet()) {
            json.add(val.getKey(), val.getValue());
        }
        return json
            .add(
                "comments",
                this.storage.xml().nodes(this.comment()).size()
        ).build();
    }

    /**
     * XPath of this element in XML tree.
     * @return XPath
     */
    @NotNull(message = "Xpath is never NULL")
    private String xpath() {
        return String.format(
            "/github/repos/repo[@coords='%s']/pulls/pull[number='%d']",
            this.coords, this.num
        );
    }

    /**
     * XPath of issue element in XML tree.
     * @return XPath
     */
    @NotNull(message = "comment is never NULL")
    private String comment() {
        return String.format(
            // @checkstyle LineLengthCheck (1 line)
            "/github/repos/repo[@coords='%s']/pulls/pull[number='%d']/comments/comment",
            this.coords, this.num
        );
    }

}
