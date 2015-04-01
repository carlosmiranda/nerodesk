/**
 * Copyright (c) 2015, nerodesk.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the nerodesk.com nor
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
package com.nerodesk.takes;

import com.nerodesk.om.Doc;
import com.nerodesk.om.Docs;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.misc.Href;
import org.takes.rq.RqHref;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * List of docs.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.2
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class TkDocs implements Take {

    /**
     * Docs.
     */
    private final transient Docs docs;

    /**
     * Request.
     */
    private final transient Request request;

    /**
     * Ctor.
     * @param dcs Docs
     * @param req Request
     */
    public TkDocs(final Docs dcs, final Request req) {
        this.docs = dcs;
        this.request = req;
    }

    @Override
    public Response act() throws IOException {
        return new RsPage(
            "/xsl/docs.xsl",
            this.request,
            new XeLink("upload", "/doc/write"),
            new XeSource() {
                @Override
                public Iterable<Directive> toXembly() throws IOException {
                    return TkDocs.this.list();
                }
            },
            new XeLink("mkdir", "/dir/create")
        );
    }

    /**
     * Convert docs into directives.
     * @return Directives
     * @throws IOException If fails
     */
    private Iterable<Directive> list() throws IOException {
        final Directives dirs = new Directives().add("docs");
        final Href home = new RqHref(this.request).href();
        for (final String name : this.docs.names()) {
            final Doc doc = this.docs.doc(name);
            final Href href = home.path("doc").with("file", name);
            dirs.add("doc")
                .add("name").set(name).up()
                .add("read").set(href.path("read").toString()).up()
                .add("delete").set(href.path("delete").toString()).up()
                .add("add-friend").set(href.path("add-friend").toString()).up()
                .add("friends");
            for (final String friend : doc.friends().names()) {
                dirs.add("friend")
                    .add("name").set(friend).up()
                    .add("eject")
                    .set(
                        href.path("eject-friend")
                            .with("friend", friend).toString()
                    )
                    .up().up();
            }
            dirs.up().up();
        }
        return dirs;
    }

}
