package com.versionlimiter.listener;

import com.liferay.portal.ModelListenerException;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.BaseModelListener;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.journal.util.comparator.ArticleVersionComparator;
import java.util.List;

public class VersionLimiterListener
  extends BaseModelListener<JournalArticle>
{
  private static final String VERSIONLIMITER_LIMIT = "version.limiter.max.number.of.versions";
  
  public void onAfterUpdate(JournalArticle model)
    throws ModelListenerException
  {
    super.onAfterUpdate(model);
    if (PropsUtil.contains("version.limiter.max.number.of.versions"))
    {
      String limitValue = PropsUtil.get("version.limiter.max.number.of.versions");
      if ((Validator.isNotNull(limitValue)) && 
        (Validator.isDigit(limitValue)))
      {
        int maxVersions = Integer.valueOf(limitValue).intValue();
        if (maxVersions > 0)
        {
          ArticleVersionComparator articleVersionComparator = new ArticleVersionComparator(
            true);
          DynamicQuery dynamicQuery = 
            JournalArticleLocalServiceUtil.dynamicQuery();
          dynamicQuery.add(RestrictionsFactoryUtil.eq("articleId", 
            model.getArticleId()));
          dynamicQuery.add(RestrictionsFactoryUtil.eq("groupId", 
            Long.valueOf(model.getGroupId())));
          try
          {
            List<JournalArticle> list = 
              JournalArticleLocalServiceUtil.dynamicQuery(dynamicQuery, -1, 
              -1, 
              articleVersionComparator);
            if (list.size() > maxVersions) {
              for (int i = 0; i < list.size() - maxVersions; i++) {
                JournalArticleLocalServiceUtil.deleteJournalArticle(((JournalArticle)list.get(i)).getId());
              }
            }
          }
          catch (Exception localException) {}
        }
      }
    }
  }
}