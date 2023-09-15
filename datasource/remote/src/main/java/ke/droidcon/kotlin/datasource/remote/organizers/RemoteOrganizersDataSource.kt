package ke.droidcon.kotlin.datasource.remote.organizers

import ke.droidcon.kotlin.datasource.remote.organizers.model.OrganizersPagedResponse
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

interface RemoteOrganizersDataSource {

    suspend fun getIndividualOrganizers(): DataResult<OrganizersPagedResponse>

    suspend fun getCompanyOrganizers(): DataResult<OrganizersPagedResponse>
}