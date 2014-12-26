package org.exoplatform.wcm.ext.component.activity;


import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.social.plugin.doc.UIDocViewer;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentConfig(
        template = "war:/groovy/ecm/social-integration/UIDocumentPreview.gtmpl",
        events = {
                @EventConfig(listeners = UIDocumentPreview.CloseActionListener.class)
        }
)
public class UIDocumentPreview extends UIContainer {

  private BaseUIActivity baseUIActivity;

  public UIDocumentPreview() throws Exception {
    this.addChild(UIDocViewer.class, null, "UIDocViewer");
    this.addChild(UIPreviewCommentArea.class, null, "UIPreviewCommentArea");
  }

  public void setContentInfo(String docPath, String repository, String workspace, Node docNode) {
    UIDocViewer uiDocViewer = this.getChild(UIDocViewer.class);
    uiDocViewer.docPath = docPath;
    uiDocViewer.repository = repository;
    uiDocViewer.workspace = workspace;
    uiDocViewer.setOriginalNode(docNode);
    uiDocViewer.setNode(docNode);
  }

  public Node getOriginalNode() throws Exception {
    UIDocViewer uiDocViewer = findFirstComponentOfType(UIDocViewer.class);
    return uiDocViewer.getOriginalNode();
  }

  private boolean isWebContent() throws Exception {
    UIDocViewer uiDocViewer = findFirstComponentOfType(UIDocViewer.class);
    Node previewNode = uiDocViewer.getNode();
    if (previewNode != null) {
      return previewNode.isNodeType(org.exoplatform.ecm.webui.utils.Utils.EXO_WEBCONTENT);
    }

    return false;
  }

  /**
   * Check if a node is media/image
   * @param data
   * @return
   * @throws Exception
   */
  private boolean isMediaFile(Node data) throws Exception {
    if (data.isNodeType(Utils.NT_FILE)) {
      UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
      List<UIExtension> extensions = manager.getUIExtensions(Utils.FILE_VIEWER_EXTENSION_TYPE);

      Map<String, Object> context = new HashMap<String, Object>();
      context.put(Utils.MIME_TYPE, data.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_MIMETYPE).getString());

      for (UIExtension extension : extensions) {
        if (manager.accept(Utils.FILE_VIEWER_EXTENSION_TYPE, extension.getName(), context)
                && !"Text".equals(extension.getName())
                && !"PDF".equals(extension.getName())) {
          return true;
        }
      }
    }

    return false;
  }

  public BaseUIActivity getBaseUIActivity() {
    return baseUIActivity;
  }

  public void setBaseUIActivity(BaseUIActivity baseUIActivity) {
    this.baseUIActivity = baseUIActivity;
  }

  public static class CloseActionListener extends EventListener<UIDocumentPreview> {
    public void execute(Event<UIDocumentPreview> event) throws Exception {
      UIDocumentPreview uiDocumentPreview = event.getSource();
      uiDocumentPreview.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentPreview.getParent());
    }
  }
}
