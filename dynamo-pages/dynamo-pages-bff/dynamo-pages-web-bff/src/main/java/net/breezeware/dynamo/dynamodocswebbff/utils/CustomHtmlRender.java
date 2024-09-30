package net.breezeware.dynamo.dynamodocswebbff.utils;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HtmlBlock;
import org.springframework.stereotype.Component;

/**
 * Custom HTML renderer for modifying fenced code blocks in Markdown content.
 * Inserts a button tag before each fenced code block, wrapping the code block
 * in pre and code elements.
 */
@Component
public class CustomHtmlRender extends AbstractVisitor {

    /**
     * Visits each fenced code block in the Markdown content and modifies it by
     * adding a button tag before it.
     * @param fencedCodeBlock The fenced code block node to be modified.
     */
    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        // Extract the code content
        String codeContent = fencedCodeBlock.getLiteral();
        // Create a new container node to hold both the button and code block
        HtmlBlock containerNode = new HtmlBlock();
        containerNode.setLiteral("<pre><button>copy</button><code>" + codeContent + "</code></pre>");
        // Replace the original code block with the container
        fencedCodeBlock.insertBefore(containerNode);
        fencedCodeBlock.unlink();
    }
}
